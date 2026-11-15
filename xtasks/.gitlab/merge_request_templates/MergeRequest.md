# Summary

* **Description**: Describe the functionality this adds/repairs/... to the software product.
* **Author(s)**: @names-of-authors
* User Story: %"User Story 1: Demonstrate usage of OAuth2" 
* Tasks:
  * Closes #<task-number>
  * Closes ...

REMINDER: The "Author(s)" entry should also contain the names of those who contributed by, e.g., pair programming. Not only the people who committed.

# Code Review Checklist

* Code reviewer(s): @name-of-reviewer
* Authors present: @names-of-present-authors
* Date: 2019-09-06
* Time: 08:00 – 08:30

## Deployment

* [ ]  The application can be deployed locally.
* [ ]  The application can be built by the pipeline.

## Comments & Documentation

* [ ]  The comments in the code are clear and useful, and mostly explain why instead of what.
* [ ]  Major changes to the functionality are documented appropriately, e.g., in the `doc` folder.

## Quality Assurance

* [ ]  All data inputs are validated before use.
* [ ]  The code is appropriately covered using unit tests, if required.
* [ ]  The code is appropriately covered using integration tests, if required.

## Clean Code

* [ ]  The code does not contain commented out code.
* [ ]  The code is free of debugging commands, such as print statements (good logging are .
* [ ]  The code uses named constants instead of magic numbers/strings.
* [ ]  The code contains sensible names for variables, methods, etc..
* [ ]  The code avoids code duplication.

/label ~"In Review"
