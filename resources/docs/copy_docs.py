import mkdocs_gen_files
import yaml
import frontmatter
import os
import markdown
from pytablewriter import MarkdownTableWriter
from bs4 import BeautifulSoup

# Move README to index unless there's already an index.md 
if(os.path.exists("/docs/docs/index.md") != True): 
  readme = open("/docs/README.md").read()
  with mkdocs_gen_files.open("index.md", "w") as f: 
    print(readme, file=f) 

# Move Library Docs
# Collect frontmatter description for overview page
libraries = []
section_name = "SDP Pipeline Libraries"
rootdir = "/docs/libraries"
for library in os.listdir(rootdir):
    file = os.path.join(rootdir, library, "README.md")
    if(os.path.exists(file)):
      lib_data = {
        "file": f"{library}.md",
        "description": "Library is missing description in README frontmatter",
        "name": None
      }
      readme = open(file).read()
      # determine description from frontmatter
      post = frontmatter.loads(readme)
      if "description" in post.keys():
        lib_data["description"] = post["description"]
      # determine library name by extracting H1 from markdown 
      ## to avoid regex... we'll convert to html and then extract first <h1> tag
      html = markdown.markdown(readme)
      soup = BeautifulSoup(html, 'html.parser')
      lib_data["name"] = soup.h1.get_text()
      # add lib_data to libraries array
      libraries.append(lib_data)
      # virtually add library README to docs/
      with mkdocs_gen_files.open(f"libraries/{section_name}/{library}.md", "w") as f:
        print(readme, file=f)

# sort libraries by name ignoring case
libraries.sort(key = lambda i: str.lower(i["name"]))

# build markdown table for overview page
table = []
for lib in libraries:
  table.append([
    f"[{lib['name']}](./{section_name}/{lib['file']})",
    lib["description"]
  ])
writer = MarkdownTableWriter(headers=["Library", "Description"], value_matrix=table)
overview_table = writer.dumps()

# Move library index
if (os.path.exists('libraries/README.md')):
  file = open("/docs/libraries/README.md").read()
  with mkdocs_gen_files.open(f"libraries/README.md", "w") as f:
    print(f"{file}\n\n## Available Libraries\n\n{overview_table}", file=f)

# Create Libraries section navigation using mkdocs-awesome-pages
pages = {
  "nav": {
    "Home": "README.md",
    section_name: [ lib["file"] for lib in libraries ]
  }
}

with mkdocs_gen_files.open("libraries/.pages", "w") as f:
  print(yaml.dump(pages), file=f)