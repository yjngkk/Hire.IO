FROM node:20-alpine

# Set working directory
WORKDIR /app

# Install dependencies
COPY package.json package-lock.json* ./
RUN npm install

# Copy the rest of the frontend code
COPY . .

# Build the Vite app
RUN npm run build

# Install a lightweight static server
RUN npm install -g serve

# Expose the port used by 'serve' 
EXPOSE 3030

# Serve the built static files
CMD ["serve", "-s", "dist", "-l", "3030"]
