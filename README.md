# Group Project 2 - Parser for CSC 4351

Authors: Kyle King, Ja Aupart, Sam Vekovius, Benjamin Gutowski

The ANTLR parser reads source code and produces a parse tree from the grammar defined in gParser.g4. The ASTBuilder.java visitor then walks that parse tree, converting each Context node into a corresponding Absyn node. 28 visitor functions were implemented to cover all grammar rules including declarations, statements, and expressions.
---


## How to run.

Clone the repository with:
```bash
git clone https://github.com/YSLja/parser.git
```

Open the downloaded project folder in Terminal.

For a single test, run the ```run.sh``` script with:
```bash
./run.sh
```

To run all 100 tests, run the ```test.sh``` script with:
```bash
./test.sh
```

And the solutions will print to ```report.txt```


