requirements:
	ansible-galaxy collection install ansible.posix
	ansible-galaxy install -f -r requirements.yml

lint:
	ansible-lint

internal: requirements
	ansible-playbook -v -i inventory/internal provision-kafka-host.yml

development: requirements
	ansible-playbook -v -i inventory/development provision-kafka-host.yml