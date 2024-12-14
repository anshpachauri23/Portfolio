class CreateSubmissions < ActiveRecord::Migration[7.2]
  def change
    create_table :submissions do |t|
      t.integer :student_id
      t.integer :homework_id
      t.string :grade
      t.text :feedback
      t.timestamps
    end
  end
  end
end
