down-local-infra:
	docker-compose down --remove-orphans -v

up-local-infra:
	docker-compose up --force-recreate --renew-anon-volumes -d

build:
	./gradlew war