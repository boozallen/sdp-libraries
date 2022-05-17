package libraries.git

public class GitSpec extends JTEPipelineSpecification {

  def Git = null

  public static class DummyException extends RuntimeException {
    public DummyException(String _message) { super( _message ); }
  }

  def setup() {
    Git = loadPipelineScriptForStep("git", "git")
    Git.getBinding().setVariable("env", [git_url_with_creds: "git_url_with_creds"])
  }

  // call(Map args)
  def "If inputs are invalid, throw error" () {
    when:
      try{Git([foo: "bar"])}catch(any){}
    then:
      1 * getPipelineMock("error")({ it =~ /Unknown git actions/ }) >> { throw new DummyException("invalid action")}
  }

  def "If no valid Action is given, throw an error" () {
    when:
      try{Git([:])}catch(any){}
    then:
      1 * getPipelineMock("error")({ it =~ /You must use an action\:/}) >> { throw new DummyException("no action given")}
  }

  def "Each Action in the args map is called as expected" () {
    setup:

    when:
      Git(actions)
    then:
      x * getPipelineMock("sh")({it =~ /^git add files$/})
      y * getPipelineMock("sh")({if (it instanceof Map) it.script =~ /^git commit -m \"${m}\"$/; else false})
      z * getPipelineMock("sh")({it =~ /^git push  git_url_with_creds/}) //note: two spaces between push and git_url
    where:
      actions                                  | x | y | z | m
      [add: "files"]                           | 1 | 0 | 0 | null
      [commit: null]                           | 0 | 1 | 0 | null
      [push: null]                             | 0 | 0 | 1 | null
      [add: "files", commit: null]             | 1 | 1 | 0 | null
      [add: "files", commit: 'commit msg']     | 1 | 1 | 0 | 'commit msg'
      [add: "files", push: null]               | 1 | 0 | 1 | null
      [commit: null, push: null]               | 0 | 1 | 1 | null
      [commit: 'commit msg', push: null]       | 0 | 1 | 1 | 'commit msg'
      [add: "files", commit: null, push: null] | 1 | 1 | 1 | null
  }

  def "Each Action's closure's resolveStrategy is set to DELEGATE_FIRST" () {

  }

  def "Each Action's closure's delegate is set to the current script" () {

  }

  // call(String action)
  def "The call method with a Map args parameter is called with args action: null" () {

  }

  // "add" action
  def "If adds argument is a String, use git add to add the file" () {

  }

  def "If add's argument is a GStringImpl, use git add to add the files" () {

  }

  def "If add's argument is anything else, assume it's an array or list of files, join the elements, and call git add" () {

  }

  // "commit" action
  def "The git config is modified appropriately before a commit is made" () {

  }

  def "Git commit is used to create a commit" () {

  }

  def "The commit is created using the given message" () {

  }

  def "If the commit message is null, use a placeholder" () {

  }

  //"push" action
  def "Git push is called" () {

  }

  def "The given flags are used when git push is called" () {

  }

  def "The git push command uses the URL supplied by withGit" () {

  }

}
