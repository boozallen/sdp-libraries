#!/bin/bash

RAW_RESULTS=$1
GRYPE_CONFIG=$2

# show whitelist count
WHITELIST_COUNT=$(cat $GRYPE_CONFIG | python3 -m yq -r '.ignore | length')
echo "${WHITELIST_COUNT} CVE(s) were whitelisted."
printf "The whitelist can be found in $GRYPE_CONFIG.\n\n"

# transform the results into an organized array
cat "$RAW_RESULTS" \
  | jq -r '
    def severity_to_number:
      {
        "Critical": 0,
        "High": 1,
        "Medium": 2,
        "Low": 3,
        "None": 4,
      }[.];

    .matches
    | map(. | {
      cve: .vulnerability.id,
      severity: .vulnerability.severity,
      package: .artifact.name,
      version: .artifact.version,
      type: .artifact.type,
      location: .artifact.locations[].path,
      url: .vulnerability.dataSource
    })
    | sort_by([(.severity | severity_to_number), .package])' \
  > transformed-results.json

# get the CVE count
CVE_COUNT=$(cat transformed-results.json | jq -r 'length')

if [ "$CVE_COUNT" -eq "0" ]
then
  echo "No CVEs detected! :)"
else
  # transform the results into table columns
  cat transformed-results.json \
    | jq -r '
      map(join("|"))
      | .[]' \
    > results.txt

  # display results as a table
  echo -e "Vulnerability|Severity|Package|Version|Type|Location|Link\n$(cat results.txt)" \
    | column -t -s "|"
fi
