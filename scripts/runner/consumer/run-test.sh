REMOTE_HOME=/home/${HOME}
TEST_HOST=$1
TEST_FILE=$2

scp "${TEST_FILE}" "${TEST_HOST}":"${REMOTE_HOME}"/current.env
ssh "${TEST_HOST}" rm -f "${REMOTE_HOME}"/block.file
