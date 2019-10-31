# Minimal makefile for Sphinx documentation
#

# You can set these variables from the command line.
SPHINXOPTS    =
SPHINXBUILD   = sphinx-build
SPHINXPROJ    = SolutionsDeliveryPlatform
SOURCEDIR     = .
BUILDDIR      = _build
LIBSREPO      = https://github.com/boozallen/sdp-libraries.git
JTEREPO       = https://github.com/boozallen/jenkins-templating-engine.git
DOCKERFILE_EXISTS := $(shell docker images -q sdp-library-testing 2> /dev/null)

.PHONY: help Makefile

# Put it first so that "make" without argument is like "make help".
help: ## Show target options
	@fgrep -h "##" $(MAKEFILE_LIST) | fgrep -v fgrep | sed -e 's/\\$$//' | sed -e 's/##//'


clean: ## removes compiled documentation
	rm -rf $(BUILDDIR)

image: ## builds the container image for documentation
	docker build . -f docs.Dockerfile -t sdp-docs

docs: ## builds documentation in _build/html
      ## run make docs live for hot reloading of edits during development
	make clean
	make image

	$(eval goal := $(filter-out $@,$(MAKECMDGOALS)))
	@if [ "$(goal)" = "live" ]; then\
		docker run -p 8000:8000 -v $(shell pwd):/app sdp-docs sphinx-autobuild -b html $(ALLSPHINXOPTS) . $(BUILDDIR)/html -H 0.0.0.0;\
	elif [ "$(goal)" = "deploy" ]; then\
		$(eval old_remote := $(shell git remote get-url origin)) \
		git remote set-url origin https://$(user):$(token)@github.com/boozallen/sdp-libraries.git ;\
		docker run -v $(shell pwd):/app sdp-docs sphinx-versioning --local-conf ./conf.py push --show-banner . gh-pages . ;\
		echo git remote set-url origin $(old_remote) ;\
		git remote set-url origin $(old_remote) ;\
	else\
		docker run -v $(shell pwd):/app sdp-docs $(SPHINXBUILD) -M html "$(SOURCEDIR)" "$(BUILDDIR)" $(SPHINXOPTS) $(O);\
	fi

test: ## Automatically runs unit tests
	@if [ -z $(DOCKERFILE_EXISTS) ] && [ "$(filter-out $@,$(MAKECMDGOALS))" = "docker" ]; then\
	  docker build -f unit_test.Dockerfile -t sdp-library-testing .;\
		docker run --rm -t -v $(shell pwd):/app -w /app sdp-library-testing gradle --no-daemon test;\
	elif [ ! -z $(DOCKERFILE_EXISTS) ] && [ "$(filter-out $@,$(MAKECMDGOALS))" = "docker" ]; then\
	  docker run --rm -t -v $(shell pwd):/app -w /app sdp-library-testing gradle --no-daemon test;\
	elif [ "$(filter-out $@,$(MAKECMDGOALS))" = "wrapper"]; then\
	  ./gradlew test;\
	else\
	  gradle --no-daemon test;\
	fi

#push:
#	make image
#	make get-remote-docs
# need to add sphinx-versioning command here when docs are ready to go public

# Catch-all target: route all unknown targets to Sphinx using the new
# "make mode" option.  $(O) is meant as a shortcut for $(SPHINXOPTS).

docker:
	@if [ "$(filterout $@,$(MAKECMDGOALS))" != "test"]; then\
	  echo "Make command $@ not found";\
	fi

wrapper:
	@if [ "$(filterout $@,$(MAKECMDGOALS))" != "test"]; then\
	  echo "Make command $@ not found";\
	fi
	
%: Makefile
	echo "Make command $@ not found"
