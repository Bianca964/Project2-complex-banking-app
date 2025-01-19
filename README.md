###### Copyright 2024 Farcasanu Bianca-Ioana 323CA

# Project 2 - Morgan Banking App
The Bank Management System is a Java-based application designed to simulate real-world banking
functionalities. It includes operations such as managing user accounts, handling transactions,
generating reports and also managing service plans and cashback systems. The project demonstrates
key object-oriented programming concepts like inheritance, polymorphism, encapsulation and
abstraction. Additionally, it employs design patterns such as Singleton, Factory, Command,
Builder and Strategy.

## Structure and packages
The project is structured into the following packages:
1. **bank** - Contains the main class of the application, `Bank`, which manages the bank's
operations, implemented as a *Singleton*. It includes classes for initializing the bank (`InitBank`)
and managing exchange rates between two currencies (`ExchangeRate`).
2. **accounts** - Contains account-related classes, such as `Account` (base class), `ClassicAccount`,
`SavingsAccount`, and the newly introduced `BusinessAccount`, which includes additional
business-specific logic. The `DiscountManager` class is responsible for handling account-related
discounts (cashback).
3. **cards** - Contains card-related classes, such as `Card` (base class) and the `OneTimeCard`
class which extends the "Card" class and provides additional logic for one-time cards.
4. **cashback** - Introduces a cashback system based on commerciants and spending. It includes:
    * the `CashbackStrategy` interface for defining cashback rules.
    * the `CashbackStrategyContext` context for selecting the appropriate strategy.
    * Strategies such as `NrOfTransactionsCashback` and `SpendingThresholdCashback`.
5. **serviceplans** - Introduces service plans for users. The main abstract class is `ServicePlan`
and its subclasses include `GoldPlan`, `SilverPlan`, `StandardPlan` and `StudentPlan`, which
offer different fees and commissions.
6. **transactions** - Includes transaction logic and extends functionality with a **splitpayments**
subpackage for splitting payments:
    * `SplitPaymentEqual`: Splits payments equally among all accounts involved.
    * `SplitPaymentCustom`:  Allows customized payment splits.
Besides these, in this package transaction tracking and history are managed by:
    * `TransactionHistory` class: contains the logic for keeping evidence of transactions for
   accounts and users ordered by timestamp. 
    * `TransactionService` class: which provides the core logic for all transaction management.
    * `Transaction`: class representing a bank transaction, using the *Builder* pattern for
   creating transactions.
7. **reports** - It is responsible for generating various types of reports related to user
accounts, transactions, and business metrics. Contains:
   * subpackage **businessreport** - includes specialized classes for generating business-related
   reports, incorporating the *Factory Design Pattern*:
      * `BusinessReportFactory`: class for creating specific types of business-related reports.
      * `BusinessReport`: report focusing on business-specific metrics.
      * `CommerciantReport`: business report summarizing merchant activity and spending
     for a business account.
      * `TransactionReport`: business report providing a detailed analysis of transactions
     for a business account.
   * `ReportGenerator` interface - acts as the foundation of the *Strategy Design Pattern*
   for report generation.
   * `ClassicReport` and `SpendingsReport` classes - provide implementations for generating
   specific reports.
8. **users** - Contains the `User` class which manages user data and their associated accounts,
transactions, and service plans.
9. **commands** - Contains the command classes organised in subpackages used for processing the
input commands.
10. **commerciants** - Contains the `Commerciant` class, which represents a merchant, used for
online payments, cashback etc.
11. **fileio** - Contains classes in which the input is stored.
12. **main** - Contains the classes `Main` and `Test` that run the program.

## Features
The application provides the following enhanced functionalities:
1. **Business Accounts** - Added for corporate clients, supporting advanced features like higher
transaction limits and integration with the cashback system for transactions. It has an owner with
privileges to manage the account and its associated users (managers and employees).
2. **Cashback System** - Introduced a cashback system based on commerciants and spending, offering
different strategies for cashback rewards.
3. **Service Plans** - Introduced service plans for users, offering different fees and commissions
based on the user's status.
4. **Transaction Splitting** - Added the ability to split payments among multiple accounts, with
two strategies available: equal split and custom split.
5. **Expanded Reports** - Enhanced the reporting system with business-specific reports, such as
commerciant reports and transaction reports.

## Design Patterns
1. **Singleton Pattern**
   * The `Bank` class is implemented as a Singleton, ensuring that only one instance of the
     bank exists.

2. **Factory Pattern** - Used in two places within the application:
   * *Command Management*: The `CommandFactory` class creates instances of commands dynamically
   based on the given input.
   * *Business Reports*: The `BusinessReportFactory` class is used to generate specific types of
   business-related reports, such as `CommerciantReport` and `TransactionReport`.

3. **Command Pattern** - Used for managing commands efficiently:
   * The `Command` abstract class defines the structure for all commands, including the execute
   method.
   * Each specific command class extends the `Command` abstract class and executes a dedicated
   operation.

4. **Builder Pattern** - Used for creating transactions in the application.
   * The `TransactionBuilder` internal class creates instances of transactions being able to
     populate only the needed fields.

5. **Strategy Pattern** - Used in two key areas:

   1. Cashback System :
     * The `CashbackStrategy` interface serves as the base for defining different cashback
   strategies, such as `NrOfTransactionsCashback` and `SpendingThresholdCashback`.
     * The `CashbackStrategyContext` class enables dynamic switching between these strategies
   depending on the commerciant type.

   2. Report Generation :
      * The `ReportGenerator` interface forms the basis for the Strategy pattern in report
      generation.
      * Implementations like `ClassicReport`, `SpendingsReport`, and `BusinessReport` define
      distinct strategies for generating specific report types.
