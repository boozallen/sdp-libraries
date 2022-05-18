fields{
  required{
    stageName = String
    buildContainer = String
  }
  optional{
    options = ArrayList
    goals = ArrayList
    phases = ArrayList
    artifacts = ArrayList
    secrets = ArrayList
  }
}