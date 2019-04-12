# Capstone Template

This code provides a template for the capstone project.

## Data

The provided data is extracted from the 5-core reviews of 'Office Products' from the Amazon
data set collected by Julian McAuley: http://jmcauley.ucsd.edu/data/amazon/

**Do not redistribute the included data files or upload them to public GitHub repositories.**

## Tasks

The `evalOfficeProducts` task runs a basic LensKit experiment over this data.

The `convertOfficeProducts` task converts the downloaded data into LensKit-compatible CSV files, which
will be placed in the `data/office-products` directory.  This is how we generated the data, and it is
included for your reference and convenience.  You should not need to run it.

## Provided Code

- The converter.
- Algorithm configurations (in `cfg`). `-explicit` algorithms use ratings; `-implicit` algorithms
  treat ratings as an implicit feedback 'has rated' signal.
- A recommender, `CategorySpreadItemRecommender`, that filters recommendations to not recommend more
  than one product in a category, to show you how to use category data in a recommender. 