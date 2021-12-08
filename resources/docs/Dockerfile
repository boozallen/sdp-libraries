FROM squidfunk/mkdocs-material:8.0.4
RUN pip install \
    mkdocs-gen-files \
    mike \
    pymdown-extensions \
    mkdocs-awesome-pages-plugin \
    pyyaml \
    python-frontmatter \
    beautifulsoup4 \
    markdown \
    pytablewriter && \
    git config --global user.name "docs deployer" && \
    git config --global user.email "null@null.com" && \
    git config --global credential.helper 'store --file=/root/.git-credentials'