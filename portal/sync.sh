#!/bin/bash
echo "sync this file"
while [ 1 -eq 1 ];do 
    src=$PWD/public/static/
    target=$PWD/build/static/
    rsync -avz $src $target &> /dev/null
    sleep 1
done 

