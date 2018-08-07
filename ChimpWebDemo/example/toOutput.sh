if [ $# -eq 0 ]
then
	echo "Provide the script to run as an argument." 
	exit -1
fi

bash $1 > tmpFile.txt
p1=$(grep ChimpDriver-Outcome tmpFile.txt | cut -d "=" -f 2)
execTrace=$(grep ChimpDriver-ExecutedTrace tmpFile.txt)
echo tmpFile.txt
(cd /chimpcheck/demoWrapper && sbt run $p1 tmpFile.txt)
rm tmpFile.txt