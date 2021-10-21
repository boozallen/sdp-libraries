import mkdocs_gen_files
import os

# Move README to index unless there's already an index.md 
if(os.path.exists("/docs/docs/index.md") != True): 
  readme = open("/docs/README.md").read()
  with mkdocs_gen_files.open("index.md", "w") as f: 
    print(readme, file=f) 

# Move Library Docs
rootdir = "/docs/libraries"
for library in os.listdir(rootdir):
    file = os.path.join(rootdir, library, "README.md")
    if(os.path.exists(file)): 
      readme = open(file).read()
      with mkdocs_gen_files.open(f"libraries/{library}.md", "w") as f:
        print(readme, file=f)

#move library index
if (os.path.exists('libraries/README.md')):
  file = open("/docs/libraries/README.md").read()
  with mkdocs_gen_files.open(f"libraries/README.md", "w") as f: 
    print(file, file=f)