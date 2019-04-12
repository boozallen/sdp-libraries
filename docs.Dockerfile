FROM python:2.7

# install documentation dependencies
RUN pip install sphinx==1.6.7               \
                sphinx-autobuild==0.7.1     \
                sphinx-rtd-theme==0.4.1     \
                recommonmark==0.4.0      && \
    pip install -U git+https://github.com/sizmailov/sphinxcontrib-versioning@conditionally_run_setup_py

        
# expectation is the container gets run with 
# docker run -v $(path to repo):/app
WORKDIR /app
