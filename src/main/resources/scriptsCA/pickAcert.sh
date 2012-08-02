#!/bin/bash
#
# Colors
ColReset="\e[m"
ColRed="\e[31m"
ColGreen="\e[32m"
ColBlue="\e[34m"
ColMagenta="\e[35m"
utilPath="/usr/bin/"
#SUBJECT="CN=Riccardo Brunetti 6,L=Torino,OU=IT"
#certCAName="CA Administrator of Instance pki-ca's IGI Domain ID"
#certCAName="RA Administrator's IGI Domain ID"
certCAName="CA Administrator of Instance pki-ca's Cnaf Domain ID"
certCApath="/var/lib/tomcat6/webapps/CAOnlineBridge/WEB-INF/classes/scriptsCA"
#
# Useful functions
#

# Wraps a given command and shows the output only if an error occurs (signalled
# by a nonzero return value). Output is colored

function Exec() {

	local Fatal=0
	local Verbose=0

	# Spaces in arguments are supported with the following syntax ($@ + eval)
	local Args=$(getopt -o fv --long fatal,verbose -- "$@")
	eval set -- "$Args"

	# Parse for verbose and fatal switches
	while true; do
		case "$1" in
			-f) Fatal=1 ;;
			-v) Verbose=1 ;;
			--) break ;;
		esac
		shift
	done
	shift # skip '--'

	local Name="$1"
	local RetVal
	local Log=$(mktemp /tmp/XXXXX)
	shift  # From $1 on, we have the full intended command

	# Command is launched and wrapped to avoid undesired output (if not verbose)
	echo -e " ${ColMagenta}*${ColReset} ${Name}..."

	if [ $Verbose == 0 ]; then
		# Swallow output
		"$@" > $Log 2>&1
		RetVal=$?
  	else
    		# Verbose
    		"$@"
    		RetVal=$?
  	fi

  	if [ $RetVal == 0 ]; then
    		# Success
    		echo -e " ${ColMagenta}*${ColReset} ${Name}: ${ColGreen}ok${ColReset}"
  	else
    		# Failure
    		echo -e " ${ColMagenta}*${ColReset} ${Name}: ${ColRed}fail${ColReset}"
		rm -rf ${TMPDIR}
    		# Show log only if non-empty
    		if [ -s $Log ]; then
      			echo "=== Begin of log dump ==="
      			cat $Log
      			echo "=== End of log dump ==="
    		fi

    		# Fatal condition
    		if [ $Fatal == 1 ]; then
			rm -rf ${TMPDIR}
      			rm -f $Log
      			exit 1
    		fi

  	fi

  	# Cleanup
  	rm -f $Log

  	# Pass return value to caller
  	return $RetVal

}

function genRandomDir() {
  	TMPDIR=`mktemp -d /tmp/XXXXXXXXXXXX` || exit 1
  	chmod 777 $TMPDIR
}

function genRandomPWD() {
        local randompassLength
        if [ $1 ]; then
                randompassLength=$1
        else
                randompassLength=8
        fi
        pass=</dev/urandom tr -dc A-Za-z0-9 | head -c $randompassLength
	echo $pass  	
}

function genDB() {
    	local retVal=0
	certpwdfile="$TMPDIR/thepwdfile"
	local thepass=`genRandomPWD`
	echo "$thepass" > $certpwdfile
  	chmod 400 $certpwdfile
  	certutil -N -f $certpwdfile -d $TMPDIR
	retVal=$?
	return $retVal
}

function getAgentCACert() {
	local retVal=0
	local p12PWD="topolino"
	${utilPath}/pk12util -i $certCApath/ca_agent.p12 -k $certpwdfile -W $p12PWD -d $TMPDIR
#	${utilPath}/pk12util -i RegistrationAuthority.p12 -k $certpwdfile -W $p12PWD -d $TMPDIR
	retVal=$?
	${utilPath}/certutil -A -d $TMPDIR -n "Certificate Authority - Cnaf Domain" \
	-t "CT,C,C" -i $certCApath/CertificateAuthority.p12
	retVal=$?
	return $retVal
}

function genNewReq() {
	echo $SUBJECT
	local retVal=0
	/bin/dd if=/dev/random of=${TMPDIR}/noise.txt count=1
	retVal=$?
#	${utilPath}/certutil -R -g 2048 -s "$SUBJECT" -7 "riccardo@pippo.pluto.com" \
	${utilPath}/certutil -R -g 2048 -s "$SUBJECT" -7 "$EMAIL" \
	-d $TMPDIR -a -f $certpwdfile -z ${TMPDIR}/noise.txt > ${TMPDIR}/certreq.tmp
	retVal=$?
	echo "-----BEGIN NEW CERTIFICATE REQUEST-----" > ${TMPDIR}/certreq.ascii
	sed -e '1,/-----BEGIN NEW CERTIFICATE REQUEST-----/d' ${TMPDIR}/certreq.tmp >> \
	${TMPDIR}/certreq.ascii
	retVal=$?
	#rm -f ${TMPDIR}/certreq.tmp
	return $retVal
}

function enrollReq() {
	local retVal=0
	local certpwd=`cat $certpwdfile`
	${utilPath}/CMCEnroll -d $TMPDIR -n "$certCAName" -r ${TMPDIR}/certreq.ascii -p $certpwd
	retVal=$?
	sed -i -e 's/-----END NEW CERTIFICATE REQUEST-----/\n-----END NEW CERTIFICATE REQUEST-----/' \
	${TMPDIR}/certreq.ascii.out
	retVal=$?
	${utilPath}/AtoB ${TMPDIR}/certreq.ascii.out ${TMPDIR}/certreq.ascii.out.bin
	retVal=$?
	return $retVal
}

function subRequest() {
	local retVal=0
	local certpwd=`cat $certpwdfile`
	echo "
#host: host name for the http server
host=openlab04.cnaf.infn.it
 
#port: port number
port=9444
 
#secure: true for secure connection, false for nonsecure connection
secure=true
 
#input: full path for the enrollment request, the content must be in binary format
input=${TMPDIR}/certreq.ascii.out.bin
 
#output: full path for the response in binary format
output=${TMPDIR}/certresp.ascii.out.bin
 
#dbdir: directory for cert8.db, key3.db and secmod.db
#This parameter will be ignored if secure=false
dbdir=$TMPDIR
 
#clientmode: true for client authentication, false for no client authentication
#This parameter will be ignored if secure=false
clientmode=true
 
#password: password for cert8.db
#This parameter will be ignored if secure=false and clientauth=false
password=$certpwd
 
#nickname: nickname for client certificate
#This parameter will be ignored if clientmode=false
nickname=$certCAName
 
#servlet: servlet name
servlet=/ca/ee/ca/profileSubmitCMCFull	
" >> ${TMPDIR}/httpclient.cfg
	${utilPath}/HttpClient ${TMPDIR}/httpclient.cfg
	retVal=0
	return $retVal
}

function getP12() {
	local retVal=0
	${utilPath}/certutil -A -n "the new cert" -t "u,u,u" -i ${TMPDIR}/certresp.ascii.out.bin \
	-d ${TMPDIR} 
	retVal=$?
	local certpwdfile2="$TMPDIR/theNewCertP12pwd"
        local thepass=`genRandomPWD`
        echo "$thepass" > $certpwdfile2
        chmod 400 $certpwdfile2
	${utilPath}/pk12util -o ${TMPDIR}/theNewCert.p12 -n "the new cert" -d ${TMPDIR} \
	-k $certpwdfile -w $certpwdfile2
	retVal=$?
	return $retVal
}

function cleanAll() {
	local retVal=0
	rm -rf ${TMPDIR}
	retVal=$?
	return $retVal
}

# Main function
function Main() {
	while getopts S:m: OPTION 
	do      case "$OPTION" in
			S) SUBJECT="$OPTARG";;
		        m) EMAIL="$OPTARG";;
			*) ;;
		esac
	done
  	Exec 'Generating a Random Temporary Working Directory' genRandomDir
  	Exec 'Creating a Certificates db' genDB
  	Exec 'Getting the Agent and CA Certificates into the db' getAgentCACert
  	Exec 'Generating a new Certificate Request' genNewReq
 	Exec 'Enrolling Certificate Request' enrollReq
	Exec 'Submitting Request to the CA Subsystem' subRequest
	Exec 'Getting final certificate in p12 format' getP12
#	Exec 'Removing all the stuff' cleanAll

	echo -e "path = "${TMPDIR}
}

Main "$@"
