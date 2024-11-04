### JIRA link



### Change description



### Work checklist


* [ ] Bug fix
* [ ] New feature
* [ ] Breaking change
* [ ] Infrastructure change

### Pull request checklist

* [ ] I have added unit tests for new code that I have added
* [ ] I have added/updated functional tests where appropriate
* [ ] The code follows our [coding standards](https://github.com/companieshouse/styleguides/blob/master/java.md)

__An exhaustive list of peer review checks can be read [here](https://github.com/companieshouse/styleguides/blob/master/java_review.md#developer-actions-prior-to-code-commitreview-started)__
### Merge instructions

We are committed to keeping commit history clean, consistent and linear. To achieve this commit should be structured as follows:

```
<type>[optional scope]: <description>
```

and contain the following structural elements:

- fix: a commit that patches a bug in your codebase (this correlates with PATCH in semantic versioning),
- feat: a commit that introduces a new feature to the codebase (this correlates with MINOR in semantic versioning),
- BREAKING CHANGE: a commit that has a footer `BREAKING CHANGE:` introduces a breaking API change (correlating with MAJOR in semantic versioning). A BREAKING CHANGE can be part of commits of any type,
- types other than `fix:` and `feat:` are allowed, for example `build:`, `chore:`, `ci:`, `docs:`, `style:`, `refactor:`, `perf:`, `test:`, and others,
- footers other than `BREAKING CHANGE: <description>` may be provided.