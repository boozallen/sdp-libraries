fields{
  required{
    stageName = String
    buildContainer = String
    phases = ArrayList
  }
  optional{
    options = ArrayList
    goals = ArrayList
    artifacts = ArrayList
    secrets = ArrayList
  }
}