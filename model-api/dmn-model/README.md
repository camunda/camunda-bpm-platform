camunda-dmn-model
==================

DMN model API written in Java.

Used DMN Version: https://github.com/omg-dmn-taskforce/omg-dmn-spec/tree/dmn11-61/xsd

## DMN10.xsd Modifications

| Id  | Commit   | Description                                      | Motivation |
|-----|----------|--------------------------------------------------|------------|
| 001 | [ab5c37] | Let tDecisionRule and tClause extend tDMNElement | Both elements should be addressable by id. Also they probably will be extended by extensions like listeners or additional attributes. |

[ab5c37]: https://github.com/camunda/camunda-dmn-model/commit/ab5c3723e4856d6e73ad5fde7bf99cb96fd8ef8d#diff-bcc24370320fcfba36757b6cebe5cb28
