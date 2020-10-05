echo "Test Script Started"

pwd

echo ${testVariable}

echo ${newVariable}

for i in `seq 1 1000`;

do

echo "Loop to test timeout ... i is $i"

done

mvn -version

echo "Test Script Ended"
