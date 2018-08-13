app1=$(cat $(dirname "$0")/${1}Info.txt | head -n 1)
app2=$(cat $(dirname "$0")/${1}Info.txt | tail -n 1)
curl -X POST "http://localhost:18010" -H "accept: application/json" -d '{"eventTrace": "'"${2}"'", "test": "'"${1}"'", "apk": "'"${app1}"'", "appPack": "'"${app2}"'" }'
