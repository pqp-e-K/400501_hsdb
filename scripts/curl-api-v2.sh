#!/bin/bash

#export http_proxy=http://18.192.78.237:8888
#export https_proxy=http://18.192.78.237:8888

#echo curl --request GET \
#--url https://deliver-test.ard.de/asset-api/items \
#--header 'Authorization: Basic dXNlcm5hbWU6cGFzc3dvcmQ='

#username="gabriel.schneider@pqp.systems"
#password="29312526LoB!"
username="deliver"
password="J9Xsbzg4SkHvjvrgpx*c"

credentials=$(echo "$username:$password" | base64 )

#requesturl="https://deliver-test.ard.de/asset-api/items/episodes"
#resource="publications/urn:ard:publication:197464508b8d21a7"
resource="$1"
requesturl="https://deliver-test.ard.de/asset-api/$resource"

#echo curl -v -x https://18.192.78.237:8888 -L "$requesturl" --header "Authorization: Basic $credentials"
#curl -v -x http://18.192.78.237:8888 -L "$requesturl" --header "Authorization: Basic $credentials"
#curl -v -x http://18.192.78.237:8888 -L "$requesturl" --header "Authorization: Basic $credentials"
#curl -v -x http://18.192.78.237:8888 -L https://www.wieistmeineip.de

#echo $credentials | base64 --decode
#echo "finish"

curl -x http://18.192.78.237:8888 -L "$requesturl" -u $username:"$password" | python3 -m json.tool