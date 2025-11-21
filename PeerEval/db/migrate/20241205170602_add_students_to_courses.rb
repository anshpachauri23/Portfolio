class AddStudentsToCourses < ActiveRecord::Migration[7.2]
  def change
    add_column :courses, :students, :json, default:[]
  end
end
