fields{
    optional{
        thresholds{
            performance{
                fail = Double
                warn = Double
            }
            accessibility {
                fail = Double
                warn = Double
            }
            best_practices{
                fail = Double
                warn = Double
            }
            search_engine_optimization{
                fail = Double
                warn = Double 
            }
        }
    }
    required{
        url = String
    }
}