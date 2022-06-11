#!/bin/bash
PWD="$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )"
java -jar $PWD/../lib/hspdb-audiothek-matching.jar "$@"
