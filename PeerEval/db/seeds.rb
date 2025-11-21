# This file should ensure the existence of records required to run the application in every environment (production,
# development, test). The code here should be idempotent so that it can be executed at any point in every environment.
# The data can then be loaded with the bin/rails db:seed command (or created alongside the database with db:setup).
#
# Example:
#
#   ["Action", "Comedy", "Drama", "Horror"].each do |genre_name|
#     MovieGenre.find_or_create_by!(name: genre_name)
#   end
student = User.create!(email: 'student@example.com', password: 'password', role: 'student')
homework = Homework.create!(title: 'Test Homework', description: 'Complete this task', due_date: Time.now + 7.days)
Submission.create!(student_id: student.id, homework_id: homework.id, grade: 'A+', feedback: 'Great job!')
