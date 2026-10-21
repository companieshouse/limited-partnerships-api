#!/usr/bin/env bash
# Builds target/generated-docs/index.html from the REST Docs snippets.
# Every snippet folder "<group>/<scenario>" becomes a section automatically,
# so new document("<group>/<scenario>") calls in tests appear without editing any .adoc.
set -euo pipefail

root="$(cd "$(dirname "$0")/.." && pwd)"
snippets="$root/target/generated-snippets"
out="$root/target/generated-docs"
adoc="$out/index.adoc"

[ -d "$snippets" ] || { echo "No snippets found. Run the tests first (mvn test)."; exit 1; }
mkdir -p "$out"

title() { echo "$1" | tr '-' ' ' | awk '{ $1=toupper(substr($1,1,1)) substr($1,2); print }'; }

cp "$root/src/docs/asciidoc/index.adoc" "$adoc"

# Snippet folders can be nested up to 5 levels:
#   "<group>/<scenario>", "<group>/<sub-title>/<scenario>", "<group>/<sub-title>/<sub-sub-title>/<scenario>", ...
# Every folder level becomes a section (group = "==", next level "===", and so on)
# and the last level is the scenario, one level below its parent.
#
# Order: at every level, items in order of first use in the test sources (document("...") calls),
# then any snippet folder not referenced by a test, alphabetically. Inside a folder the scenarios
# come before its sub-titles, and "success*" scenarios/sub-titles come first at every level.
# Note: only literal document("...") names are found in the tests; names built at runtime
# (e.g. document(variable)) are unknown here and fall back to alphabetical order.
ordered=$( { grep -rhoE 'document\("[^"]+"' "$root/src/test/java" | sed -E 's/document\("//; s/"$//'
             (cd "$snippets" && find . -name http-request.adoc | sed -E 's|^\./||; s|/http-request\.adoc$||' | sort); } \
           | awk '!seen[$0]++' \
           | while read -r d; do [ -f "$snippets/$d/http-request.adoc" ] && echo "$d"; done \
           | awk -F/ '{
               key = ""; path = ""
               for (i = 1; i < NF; i++) {
                 path = path "/" $i
                 if (!(path in order)) { order[path] = ++n }
                 folder_ok = ($i ~ /^success/) ? 0 : 1
                 key = key sprintf("1%d%05d/", folder_ok, order[path])
               }
               ok = ($NF ~ /^success/) ? 0 : 1
               print key sprintf("0%d%05d", ok, NR) "\t" $0 }' \
           | LC_ALL=C sort -t "$(printf '\t')" -k1,1 | cut -f2 )

previous=()
for dir in $ordered; do
  IFS=/ read -r -a parts <<< "$dir"
  last=$(( ${#parts[@]} - 1 ))
  if [ "$last" -gt 4 ]; then
    echo "Skipping $dir: sections can be nested up to 5 levels only." >&2
    continue
  fi

  # Print a heading for every parent level that differs from the previous scenario's.
  changed=0
  for (( i = 0; i < last; i++ )); do
    if [ "$changed" -eq 1 ] || [ "${previous[$i]:-}" != "${parts[$i]}" ]; then
      changed=1
      printf '\n%s %s\n' "$(printf '=%.0s' $(seq 1 $((i + 2))))" "$(title "${parts[$i]}")" >> "$adoc"
    fi
  done
  previous=("${parts[@]:0:last}")

  printf '\n%s %s\n' "$(printf '=%.0s' $(seq 1 $((last + 2))))" "$(title "${parts[$last]}")" >> "$adoc"
  for snippet in http-request http-response curl-request; do
    if [ -f "$snippets/$dir/$snippet.adoc" ]; then
      printf '\n.%s\ninclude::%s/%s/%s.adoc[]\n' "$(title "$snippet")" "$snippets" "$dir" "$snippet" >> "$adoc"
    fi
  done
done

asciidoctor -D "$out" "$adoc"
echo "Generated $out/index.html"



