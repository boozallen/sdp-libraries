FROM squidfunk/mkdocs-material
RUN pip install \
    mkdocs-gen-files \
    mkdocs-awesome-pages-plugin \
    mike \
    pymdown-extensions && \
    git config --global credential.helper 'store --file=/root/.git-credentials'