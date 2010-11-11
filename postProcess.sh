#!/bin/bash -vxe
#!/bin/bash -e

######
# Output format: one ';'-separated line with:
# - filename stem (without extension)
# - last line of macroDebug.txt (5 fields);
# - last line of debugsource.txt (3 fields);
# - <lines, chars> for each of
#   <outPartialPreproc, outPreproc, outPartialPreprocThenPreproc>
# - <real, user, sys> time for preprocessor execution
#####

if [ -z "$jcppConfLoaded" ]; then
  source jcpp.conf
fi
inp=$1
shift
echo "Analyzing results of preprocessing $inp"

. setupOutPaths.sh.inc

echo -n "$outBase;" >> "$outCSV"
echo -n "$(tail -1 "$outMacroDebug");" >> "$outCSV"
echo -n "$(tail -1 "$outDebugSource");" >> "$outCSV"

filterWC() {
  #Output: lines, then bytes
  #wc -cl|$sed -r -e 's/^\s+//; s/\s+/, /g; s/$/, /'
  wc -lc|$sed -r -e 's/^\s+//; s/\s+/;/g; s/$/;/'
}

countWordLines() {
  preprocOut="$1"
  echo -n $(grep -v '^$' "$preprocOut"|filterWC) >> "$outCSV"
}

echo "=="
echo "==Preprocess source"
echo "=="
gcc $gccOpts -E "$inp" "$@" > "$outPreproc" || true

echo "=="
echo "==Preprocess output of partial preprocessor"
echo "=="
gcc $gccOpts -E "$outPartialPreproc" "$@" > "$outPartialPreprocThenPreproc" || true

#echo "Output size stats - partial preprocessor:"
countWordLines "$outPartialPreproc"

#echo "Output size stats - preprocessor:"
countWordLines "$outPreproc"

#echo "Output size stats - partial preprocessor then preprocessor:"
countWordLines "$outPartialPreprocThenPreproc"

echo -n $(cat $outTime | $sed -nre '/^(real|user|sys)\s/ s/m/ /p' |
awk '{printf "%f;", $2 * 60 + $3}'|$sed -e 's/;$//') >> "$outCSV"
# awk '{print $1, $2 * 60 + $3}'

echo >> "$outCSV"

# Remove dashed and empty lines before diffing.
excludeLines='^(#|$)'
spacesToNewLine='s/[ 	]\+/\n/g'
# -w ignores white space, -B blank line, -u helps readability.
res=0; diff -uBw <(egrep -v "$excludeLines" "$outPartialPreprocThenPreproc"| \
  $sed -e "$spacesToNewLine") <(egrep -v "$excludeLines" "$outPreproc"| \
  $sed -e "$spacesToNewLine") > "$outDiff" || res=$?

#echo $res
#if [ $res -ne 0 ]; then
#  echo "*** WARNING! - $outDiff not empty, inconsistency detected ***"
#  echo "*** The output of the preprocessor is different if it is run on the original input or on the partially preprocessed file"
#else
#  echo "Output matches"
#fi

