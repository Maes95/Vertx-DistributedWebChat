if [ "$#" -lt  "3" ]
 then
   # Use: ./deploy MyPem.pem ec2-XXX-XXX-XXX-XXX.eu-west-1.compute.amazonaws.com myJar.jar
   echo "Use: ./deploy PEM DNS JAR"
fi

PEM=$1
DNS=$2
FILE=$3

TARGET="ubuntu@${DNS}:/home/ubuntu/"

echo "${TARGET}installDependencies.sh"

scp -i $PEM installDependencies.sh "${TARGET}installDependencies.sh"
scp -i $PEM $FILE "${TARGET}Node.jar"
ssh -i $PEM "ubuntu@${DNS}" chmod +x "installDependencies.sh"
ssh -i $PEM "ubuntu@${DNS}" ./installDependencies.sh
ssh -i $PEM "ubuntu@${DNS}" "pkill -f java"
ssh -i $PEM "ubuntu@${DNS}" java -jar Node.jar -cluster


# FILE=Vertx-DistributedWebChat/target/WebChatVertxMaven-0.1.0-fat.jar
#
# # NODE 1
# scp -i $PEM $FILE ubuntu@ec2-54-154-158-98.eu-west-1.compute.amazonaws.com:/home/ubuntu/WebChatVertxMaven-0.1.0-fat.jar
# # NODE 2
# scp -i $PEM $FILE ubuntu@ec2-54-154-158-98.eu-west-1.compute.amazonaws.com:/home/ubuntu/WebChatVertxMaven-0.1.0-fat.jar
# target/WebChatVertxMaven-0.1.0-fat.jar
