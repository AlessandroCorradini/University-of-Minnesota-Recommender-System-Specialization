package org.lenskit.mooc;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.google.auto.service.AutoService;
import com.google.common.base.Preconditions;
import it.unimi.dsi.fastutil.longs.Long2DoubleMap;
import it.unimi.dsi.fastutil.longs.Long2DoubleOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongArrays;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.Namespace;
import org.lenskit.cli.Command;
import org.lenskit.util.table.TableLayout;
import org.lenskit.util.table.TableLayoutBuilder;
import org.lenskit.util.table.writer.CSVWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;

import static net.sourceforge.argparse4j.impl.Arguments.storeTrue;

@AutoService(Command.class)
public class TranslateAmazon implements Command {
    private static final Logger logger = LoggerFactory.getLogger(TranslateAmazon.class);

    @Override
    public String getName() {
        return "translate-amazon";
    }

    @Override
    public String getHelp() {
        return "Translates Amazon data into LensKit-compatible formats.";
    }

    @Override
    public void configureArguments(ArgumentParser parser) {
        parser.addArgument("ratings")
              .metavar("RATING_FILE")
              .type(File.class)
              .required(true)
              .help("Load ratings from RATING_FILE");
        parser.addArgument("metadata")
              .metavar("META_FILE")
              .type(File.class)
              .required(true)
              .help("Load metadata from META_FILE");
        parser.addArgument("--use-timestamps")
              .action(storeTrue())
              .setDefault(false)
              .help("use timestamps");
        parser.addArgument("--output-directory", "-d")
              .dest("output")
              .type(File.class)
              .metavar("DIR")
              .help("Write to output directory DIR");
    }

    @Override
    public void execute(Namespace options) {
        File inFile = options.get("ratings");
        File metaFile = options.get("metadata");
        Preconditions.checkArgument(inFile != null, "no input file");
        File outDir = options.get("output");
        if (outDir == null) {
            outDir = new File(".");
        }

        Translator tx = new Translator(options.getBoolean("use_timestamps"));
        try {
            tx.translate(inFile.toPath(), metaFile.toPath(), outDir.toPath());
        } catch (IOException e) {
            throw new RuntimeException("Error processing ratings", e);
        }
    }

    private static class Translator {
        boolean useTimestamps;

        TableLayout ratingLayout;
        TableLayout userLayout;
        TableLayout itemLayout;

        Translator(boolean usets) {
            useTimestamps = usets;

            TableLayoutBuilder rtlb = new TableLayoutBuilder();
            rtlb.addColumns("user", "item", "rating");
            if (usets) {
                rtlb.addColumn("timestamp");
            }
            ratingLayout = rtlb.build();

            userLayout = new TableLayoutBuilder().addColumns("id", "az_uid").build();
            itemLayout = new TableLayoutBuilder().addColumns("id", "asin").build();
        }

        void translate(Path in, Path meta, Path out) throws IOException {
            Files.createDirectories(out);
            Map<String,Integer> userIdMap = new HashMap<>();
            Map<String,Integer> itemIdMap = new HashMap<>();
            int nratings = 0;
            Long2IntOpenHashMap counts = new Long2IntOpenHashMap();

            logger.info("processing reviews from {}", in);
            try (InputStream raw = Files.newInputStream(in);
                 InputStream decompressed = new GZIPInputStream(raw);
                 BufferedReader read = new BufferedReader(new InputStreamReader(decompressed));
                 CSVWriter ratings = CSVWriter.open(out.resolve("ratings.csv").toFile(), ratingLayout);
                 CSVWriter users = CSVWriter.open(out.resolve("users.csv").toFile(), userLayout);
                 CSVWriter items = CSVWriter.open(out.resolve("items.csv").toFile(), itemLayout)) {

                ObjectMapper mapper = new ObjectMapper();
                ObjectReader reader = mapper.reader()
                                            .withFeatures(JsonParser.Feature.ALLOW_SINGLE_QUOTES,
                                                          JsonParser.Feature.ALLOW_BACKSLASH_ESCAPING_ANY_CHARACTER)
                                            .forType(ReviewBean.class);
                String line;
                while ((line = read.readLine()) != null) {
                    nratings += 1;

                    ReviewBean review = reader.readValue(line);

                    Integer uid = userIdMap.get(review.reviewerID);
                    if (uid == null) {
                        uid = userIdMap.size() + 1;
                        users.writeRow(uid, review.reviewerID);
                        userIdMap.put(review.reviewerID, uid);
                    }
                    Integer iid = itemIdMap.get(review.asin);
                    if (iid == null) {
                        iid = itemIdMap.size() + 1;
                        items.writeRow(iid, review.asin);
                        itemIdMap.put(review.asin, iid);
                    }
                    counts.addTo(iid.longValue(), 1);
                    if (useTimestamps) {
                        ratings.writeRow(uid, iid, review.rating, review.timestamp);
                    } else {
                        ratings.writeRow(uid, iid, review.rating);
                    }
                }
            }
            logger.info("converted {} ratings from {} users for {} items",
                        nratings, userIdMap.size(), itemIdMap.size());
            logger.info("most-rated item counted {} times",
                        counts.values().stream().max(Integer::compareTo).orElse(null));

            long[] itemIds = counts.keySet().toLongArray();
            LongArrays.quickSort(itemIds, (l1, l2) -> Integer.compare(counts.get(l1), counts.get(l2)));
            Long2DoubleMap ranks = new Long2DoubleOpenHashMap();
            double nm1 = itemIds.length - 1;
            for (int i = 0; i < itemIds.length; i++) {
                ranks.put(itemIds[i], i / nm1);
            }

            // translate metadata
            int nitems = 0;
            logger.info("processing metadata from {}", meta);
            try (InputStream raw = Files.newInputStream(meta);
                 InputStream decompressed = new GZIPInputStream(raw);
                 BufferedReader bufRead = new BufferedReader(new InputStreamReader(decompressed));
                 BufferedWriter writer = Files.newBufferedWriter(out.resolve("items.json"))) {

                ObjectMapper mapper = new ObjectMapper();
                ObjectReader reader = mapper.reader()
                                            .withFeatures(JsonParser.Feature.ALLOW_SINGLE_QUOTES,
                                                          JsonParser.Feature.ALLOW_BACKSLASH_ESCAPING_ANY_CHARACTER)
                                            .forType(MetaBean.class);
                ObjectWriter jsonWriter = mapper.writer().without(JsonGenerator.Feature.AUTO_CLOSE_TARGET);

                String line;
                while ((line = bufRead.readLine()) != null) {
                    MetaBean data = reader.readValue(line);
                    data.id = itemIdMap.get(data.asin);
                    if (data.id != null) {
                        data.availability = ranks.get(data.id.longValue());
                        nitems += 1;
                        jsonWriter.writeValue(writer, data);
                        writer.newLine();
                    }
                }
            }
            logger.info("processed metadata for {} items", nitems);
        }
    }

    @JsonIgnoreProperties({"reviewTime", "reviewText", "summary", "helpful", "reviewerName"})
    public static class ReviewBean {
        public String reviewerID;
        public String asin;
        @JsonProperty("overall")
        public Double rating;
        @JsonProperty("unixReviewTime")
        public Long timestamp;
    }

    @JsonIgnoreProperties({"related"})
    public static class MetaBean {
        public Integer id;
        public String asin;
        @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
        public String title;
        public Double availability;
        public String description;
        public Double price;
        public String imUrl;
        public Map<String,Integer> salesRank;
        public String brand;
        public List<List<String>> categories;

        @JsonProperty(access= JsonProperty.Access.READ_ONLY)
        public String getName() {
            return title;
        }
    }
}
