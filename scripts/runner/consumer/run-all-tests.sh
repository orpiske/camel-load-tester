TEST_HOST=$1
TEST_SET=$2
DATA_DIR=$3

function sendNotification() {
  which notify-pushover &> /dev/null
  if [ $? -eq 0 ] ; then
    notify-pushover "$1"
  else
    which notify-send  &> /dev/null
    if [ $? -eq 0 ] ; then
      notify-send -t 2000 "$1"
    else
      echo "$1"
    fi
  fi
}

function runSingleTest() {
	TEST_FILE=$1
	ssh "${TEST_HOST}" test -f /home/"${USER}"/current.env
	isRunning=$?

	echo -e "\n"
	while [[ $isRunning -eq 0 ]] ; do
		echo -e -n "\rCannot run because there is a test in progress"
		sleep 15

		ssh "${TEST_HOST}" test -f /home/"${USER}"/current.env
		isRunning=$?
	done

	echo -e "\nRunning: ${TEST_FILE}"
	sendNotification "Starting test ${TEST_FILE}"
	./run-test.sh "${TEST_HOST}" "${TEST_FILE}"
}

function runTest() {
	if [[ -d ${TEST_SET} ]] ; then
		for file in ${TEST_SET}/*; do
			runSingleTest "${file}"
		done
	else
		if [[ -f ${TEST_SET} ]] ; then
			runSingleTest "${TEST_SET}"
		fi
	fi

	ssh "${TEST_HOST}" test -f /home/"${USER}"/current.env
	isRunning=$?

	startTime=$(date)
	echo "Start time: ${startTime}"
	echo "Waiting for the last test to complete"
	while [[ $isRunning -eq 0 ]] ; do
		currentTime=$(date)
		echo -e -n "\rWaiting for the last test to complete: ${currentTime}"
		sleep 15

		ssh "${TEST_HOST}" test -f /home/"${USER}"/current.env
		isRunning=$?
	done
}

runTest
echo -e "\r"
DEST=${DATA_DIR}/$(basename -s .env "${TEST_SET}")
mkdir -p "${DEST}"
rsync -avr "${TEST_HOST}":/home/"${USER}"/test-data/ "${DEST}"
