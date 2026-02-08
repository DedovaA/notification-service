# --- ЭТАП 1: СБОРКА (BUILDER) ---
# Используем образ с Maven и JDK 21 для компиляции
FROM maven:3.9.6-eclipse-temurin-21-alpine AS build

# Указываем рабочую директорию для сборки
WORKDIR /app

# Сначала копируем только pom.xml.
# Это хитрость: Docker закеширует зависимости и не будет качать их заново, пока ты не изменишь pom.xml
COPY pom.xml .
RUN mvn dependency:go-offline

# Теперь копируем исходный код
COPY src ./src

# Собираем проект, пропуская тесты для скорости
RUN mvn clean package -DskipTests


# --- ЭТАП 2: ЗАПУСК (FINAL) ---
# Берем легкий образ только с JRE 21 (Runtime)
FROM eclipse-temurin:21-jre-alpine

# Указываем рабочую директорию, где будет лежать приложение
WORKDIR /app

# Копируем ТОЛЬКО готовый артефакт из этапа 'builder'
# Мы переименовываем его в app.jar для простоты запуска
COPY --from=build /app/target/*.jar app.jar

# Настраиваем запуск приложения
ENTRYPOINT ["java", "-jar", "app.jar"]
