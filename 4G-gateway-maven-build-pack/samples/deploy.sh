#!/bin/sh

environment=$1
currDir=$PWD
username=$2
if [[ -z "$3" ]]; then
  echo "Enter your password for the Apigee Enterprise, followed by [ENTER]:"
  read -s password
else
  password=$3
fi
mvn apigee-enterprise:deploy -P$environment -Dusername=$username -Dpassword=$password
