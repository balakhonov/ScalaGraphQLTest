Use the following libraries:
- Sangria
- Http4s
- Sttp
- Cats/Cats-Effect
- Zio
- Scala-scrapper
- Quill + Mysql

Abstract the effect: Done
- MainZIO for ZIO
- MainCatsIO for Cats Effect

Problems/Shortcomings:
1. Http4s uses Cats Effect in the core. So API Server always works with Cats.
2. So high abstraction not allows normally work with Async operations. Not able to convert Future to F[__] in one place in a code that relates to GraphQL execution. It can be resolved with mixing new Monades.
3. Cancelable Monad can be mixed F[__] to shutdown DB SessionFactory on application exit.
4. Better Logger can be mixed.


Routes:
1. Grab news from external site
POST /grabContent

2. Send a query to GraphQL
POST /graphql

3. Load data from DB
GET /news


Manual Test case
1. Grab the data using '/grabContent'. It will be saved to a DB.
2. Send a query to GraphQL using '/graphql'.

Unit Test coverage avg 40% (can be improved up to 90%)