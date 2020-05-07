# Minimal makefile to build Antora documentation
BUILDDIR = docs/html
PLAYBOOK = docs/antora-playbook-local.yml 

# Put it first so that "make" without argument is like "make help".
help: ## Show target options
	@fgrep -h "##" $(MAKEFILE_LIST) | fgrep -v fgrep | sed -e 's/\\$$//' | sed -e 's/##//'

clean: ## removes remote documentation and compiled documentation
	rm -rf $(BUILDDIR)/** target bin

npm: ## build npm image 
	docker build scripts/npm -t sdp-libs:npm-$(shell git rev-parse HEAD)

.PHONY: docs test
.ONESHELL:
docs: clean npm## builds the antora documentation 
	docker run \
	-v ~/.git-credentials:/home/antora/.git-credentials \
	-v $(shell pwd):/docs \
	sdp-libs:npm-$(shell git rev-parse HEAD) /node_modules/.bin/antora generate --fetch --to-dir $(BUILDDIR) $(PLAYBOOK)

test: ## runs the plugin's test suite 
	docker run \
	-v $(shell pwd):/app \
	-w /app \
	gradle:6.3.0-jdk8 gradle --no-daemon test 

# Catch-all target: route all unknown targets to Sphinx using the new
# "make mode" option.  $(O) is meant as a shortcut for $(SPHINXOPTS).
%: Makefile
	echo "Make command $@ not found" 