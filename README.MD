# MoneyWise

**MoneyWise** is a simple CLI application to track your personal finances.  
It helps you add incomes and expenses, view history, and see summaries — all from the terminal.

Built with **pure Java** and **Maven**.

---

## Quick Start

### 🔧 Requirements

- Java 21 or higher
- Maven 3.8+
- Or Docker if you wanna try it

### Installation

```bash
git clone https://github.com/timebetov/finance-tracker-cli.git
cd finance-tracker-cli
mvn clean package
java -jar target/moneywise.jar -U <username>
```
### Using Docker
```bash
cd finance-tracker-cli
docker build -t finance-tracker-cli .
docker run -it finance-tracker-cli <username>
```