#!/bin/bash
graph=$1
nb_partition=$2
lines=`wc $graph | awk '{print $1}'`
rest=$(($lines%$nb_partition))
part=$(($lines/$nb_partition))
split -l $part $graph  --numeric-suffixes=1 --suffix-length=1 
prefix="x";

dirlist=`ls ${prefix}*`;

i=0
for filelist in $dirlist
do
    #echo $filelist
     array[ $i ]="$filelist"        
    (( i++ ))
   
done
if [ $i+1 > nb_partition ]
then
#cat ${array[$nb_partition]} ${array[$nb_partition-1]} >> ${array[$nb_partition+1]}
cat ${array[$nb_partition]} >> ${array[$nb_partition-1]}
rm ${array[$nb_partition]}
fi

