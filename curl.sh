echo "Request URL: $1"
curl -w "@curl-format.txt" -o /dev/null -s "$1"
