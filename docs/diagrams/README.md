# Architecture Diagrams

This directory contains Mermaid diagrams for the Accounting Ledger Service 4+1 architectural view.

## How to View the Diagrams

### Option 1: GitHub (Recommended)
GitHub automatically renders Mermaid diagrams in Markdown files. Simply view the `.md` files in the `docs/` directory and the diagrams will be displayed inline.

### Option 2: Mermaid Live Editor
1. Copy the content from any `.mmd` file
2. Paste it into [Mermaid Live Editor](https://mermaid.live/)
3. The diagram will render automatically

### Option 3: VS Code Extension
1. Install the "Mermaid Preview" extension in VS Code
2. Open any `.mmd` file
3. Use the preview feature to view the diagram

### Option 4: Command Line
```bash
# Install Mermaid CLI
npm install -g @mermaid-js/mermaid-cli

# Generate PNG from Mermaid file
mmdc -i logical-view.mmd -o logical-view.png
```

## Diagram Files

### Logical View
- `logical-view.mmd` - System decomposition and component relationships

### Process View
- `process-view-creation.mmd` - Transaction creation flow
- `process-view-retrieval.mmd` - Transaction retrieval flow
- `process-view-feature-disabled.mmd` - Feature flag disabled flow

### Physical View
- `physical-view.mmd` - Deployment architecture and infrastructure

### Development View
- `development-view.mmd` - Package structure and module organization

### Use Case Scenarios
- `use-case-creation.mmd` - Detailed transaction creation scenario
- `use-case-retrieval.mmd` - Detailed transaction retrieval scenario
- `use-case-feature-disabled.mmd` - Feature flag disabled scenario
- `use-case-invalid-data.mmd` - Invalid data handling scenario
- `use-case-health-check.mmd` - System health check scenario

## Diagram Types Used

- **Graph Diagrams**: For showing component relationships and system structure
- **Sequence Diagrams**: For showing runtime behavior and interaction flows

## Updating Diagrams

When making changes to the system architecture:

1. Update the relevant `.mmd` file
2. Test the diagram in Mermaid Live Editor
3. Update the corresponding documentation in the `docs/` directory
4. Commit both the diagram and documentation changes together
