# Библиотека
Простейшее CRUD приложение для реализации библиотеки на Scala с использованием Play Framework.

## Запуск
Запустить приложение из терминала в каталоге проекта:
```bash
sbt run
```
И перейти по ссылке: [http://localhost:9000/library](http://localhost:9000/library)

## База данных
База данных библиотеки содержит две коллекции: авторы и книги. Связь двух коллекций организована посредством связи многие ко многим.
Для реализации был использован MongoDB Scala Driver.

**Пример коллекции книг:**
![Таблица книг](https://github.com/timbread/library/tree/master/public/db-example/books.png)

**Пример коллекции авторов:**
![Таблица авторов](https://github.com/timbread/library/tree/master/public/db-example/authors.png)


