import subprocess
import json
import time
import urllib.request
import os

pem="scripts/Vertx.pem"
jar_file="target/WebChatVertxMaven-0.1.0-fat.jar"
groupName="VertxCluster"
count=1

def url_is_alive(dns):
    """
    Checks that a given URL is reachable.
    :param url: A URL
    :rtype: bool
    """
    request = urllib.request.Request("http://%s:8080" % dns)
    request.get_method = lambda: 'HEAD'

    try:
        urllib.request.urlopen(request)
        return True
    except urllib.error.URLError:
        return False


def run(pem, dns, jar_file):
    print("RUNNING %s" % dns)
    outfile = open('logs/%s-log.log' % dns, 'w')
    subprocess.call("./scripts/deploy.sh %s %s %s &" % (pem, dns, jar_file), shell=True, stdout=outfile, stderr=outfile)
    with open(os.devnull, "w") as f:
        subprocess.call("./scripts/addServerToHA.sh node_%s %s &" % (dns, dns), shell=True, stdout=f, stderr=f)


subprocess.call("rm haproxy/haproxy.cfg", shell=True)
res=json.loads(subprocess.Popen("aws ec2 describe-instances --filter Name=\"instance.group-name\",Values=\"%s\"" % groupName, shell=True, stdout=subprocess.PIPE).stdout.read())
have_master=False

nodes = []
master = None

for instance in res['Reservations'][0]['Instances']:

    node= dict()
    node['DNS'] = instance['PublicDnsName']
    node['PRIVATE_IP'] = instance['PrivateIpAddress']
    node['PUBLIC_IP'] = instance['PublicIpAddress']
    # ONLY FIRST
    if not have_master:
        have_master = True
        subprocess.call("sed 's/$INTERFACE/%s/' src/main/resources/base.xml > src/main/resources/cluster.xml" % instance['PrivateIpAddress'], shell=True)
        print("Running: mvn install")
        subprocess.call("mvn install", shell=True, stdout=subprocess.PIPE)
        run(pem, node['DNS'], jar_file)
        node['isMaster'] = True
        master = node
    # OTHERS
    else:
        node['isMaster'] = False
    nodes.append(node)

with open('logs/instances.json', 'w') as outfile:
    json.dump(nodes, outfile)


while True and len(nodes) > 0:
    print("DEPLOYING MASTER ...")
    if url_is_alive(master['DNS']):
        break
    time.sleep( 10 )

print("Master UP")

for node in nodes:
    if not node['isMaster']:
        run(pem, node['DNS'], jar_file)

for node in nodes:
    if not node['isMaster']:
        while True and len(nodes) > 0:
            if url_is_alive(node['DNS']):
                break
            time.sleep( 10 )
        print("NODE: "+node['DNS']+" is UP")

# outfile_ha = open('logs/haproxy.txt', 'w')
# subprocess.call("haproxy -f haproxy/haproxy.cfg", shell=True, stdout=outfile_ha, stderr=outfile_ha)
