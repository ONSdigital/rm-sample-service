#!/usr/bin/env bash

usage() {
 echo "Usage: $0 [-h <string:url>] [-u <string:user>] [-p <int:password>] [-f <string:file>]"
 echo
 echo "Example: $0 -h http://localhost:8125 -u user -p password -f social-survey-sample.csv"
 exit 1;
 }

while getopts ":h:u:p:f:" o; do
    case "${o}" in
        h)
            host=${OPTARG}
            ;;
        u)
            user=${OPTARG}
            ;;
        p)
            password=${OPTARG}
            ;;
        f)
            file=${OPTARG}
            ;;
        *)
            usage
            ;;
    esac
done


if [ -z "${host}" ] || [ -z "${user}" ] || [ -z "${password}" ] || [ -z "${file}" ]; then
    usage
fi

curl -u "${user}":"${password}" -F "file=@${file}" "${host}"/samples/SOCIAL/fileupload