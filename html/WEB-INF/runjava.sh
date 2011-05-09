#
# Determine location where script is run
pathname=$(cd ${0%/*} && echo $PWD/${0##*/})
pathonly=`dirname "$pathname"`
#
# Run passed command. Include Tomcat and local lib as extension dirs (ext.dirs).
echo $pathonly
java -Djava.ext.dirs="$pathonly/lib:/opt/tomcat/common/lib" $@
