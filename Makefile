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

# Put it first so that "make" without argument is like "make help".
help:
	@$(SPHINXBUILD) -M help "$(SOURCEDIR)" "$(BUILDDIR)" $(SPHINXOPTS) $(O)

.PHONY: help Makefile

# cleanup
clean: 
	rm -rf $(BUILDDIR) pages/libraries pages/jte

# build image 
image: 
	docker build . -t sdp-docs

# build docs 
docs: 
	make image
	make get-remote-docs
	docker run -v $(shell pwd):/app sdp-docs $(SPHINXBUILD) -M html "$(SOURCEDIR)" "$(BUILDDIR)" $(SPHINXOPTS) $(O)

# hot reload
live:
	make image
	make get-remote-docs
	docker run -p 8000:8000 -v $(shell pwd):/app sdp-docs sphinx-autobuild -b html $(ALLSPHINXOPTS) . $(BUILDDIR)/html -H 0.0.0.0

push: 
	make image 
	make get-remote-docs
	# need to add sphinx-versioning command here when docs are ready to go public

# Catch-all target: route all unknown targets to Sphinx using the new
# "make mode" option.  $(O) is meant as a shortcut for $(SPHINXOPTS).
%: Makefile
	echo "Make command $@ not found" 