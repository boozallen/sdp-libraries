# Minimal makefile to build Antora documentation
BUILDDIR = docs/html
PLAYBOOK = docs/antora-playbook-local.yml 

# Put it first so that "make" without argument is like "make help".
help: ## Show target options
	@fgrep -h "##" $(MAKEFILE_LIST) | fgrep -v fgrep | sed -e 's/\\$$//' | sed -e 's/##//'

clean: ## removes remote documentation and compiled documentation
	rm -rf $(BUILDDIR)/**

.ONESHELL:
install:  ## installs the project's npm dependencies
	if [ ! -d "node_modules" ]; then npm i; fi

.PHONY: docs
docs: clean install ## builds the antora documentation 
	$(shell npm bin)/antora generate --fetch --to-dir $(BUILDDIR) $(PLAYBOOK)

test: ## runs the plugin's test suite 
	gradle clean test 

# Catch-all target: route all unknown targets to Sphinx using the new
# "make mode" option.  $(O) is meant as a shortcut for $(SPHINXOPTS).
%: Makefile
	echo "Make command $@ not found" 