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

# Order: groups in order of first use in the test sources; inside a group "success" first,
# then the remaining scenarios as the document("<group>/<scenario>") calls appear in the tests,
# then any snippet folder not referenced by a test, alphabetically.
ordered=$( { grep -rhoE 'document\("[^"]+"' "$root/src/test/java" | sed -E 's/document\("//; s/"$//'
             (cd "$snippets" && find . -mindepth 2 -maxdepth 2 -type d | sed 's|^\./||' | sort); } \
           | awk '!seen[$0]++' \
           | while read -r d; do [ -d "$snippets/$d" ] && echo "$d"; done \
           | awk -F/ '{ if (!($1 in order)) { order[$1]=++n }; ok = ($2 == "success") ? 0 : 1; print order[$1] "\t" ok "\t" NR "\t" $0 }' \
           | sort -t "$(printf '\t')" -k1,1n -k2,2n -k3,3n | cut -f4 )

previous_group=""
for dir in $ordered; do
  group="${dir%%/*}"
  scenario="${dir#*/}"

  if [ "$group" != "$previous_group" ]; then
    printf '\n== %s\n' "$(title "$group")" >> "$adoc"
    previous_group="$group"
  fi

  printf '\n=== %s\n' "$(title "$scenario")" >> "$adoc"
  for snippet in http-request http-response curl-request; do
    if [ -f "$snippets/$dir/$snippet.adoc" ]; then
      printf '\n.%s\ninclude::%s/%s/%s.adoc[]\n' "$(title "$snippet")" "$snippets" "$dir" "$snippet" >> "$adoc"
    fi
  done
done

asciidoctor -D "$out" "$adoc"
echo "Generated $out/index.html"



