import { Briefcase, Code, User } from "lucide-react";

export const AboutSection = () => {
  return (
    <section id="about" className="py-24 px-4 relative">
      {" "}
      <div className="container mx-auto max-w-5xl">
        <h2 className="text-3xl md:text-4xl font-bold mb-12 text-center">
          About <span className="text-primary"> Me</span>
        </h2>

        <div className="grid grid-cols-1 md:grid-cols-2 gap-12 items-center">
          <div className="space-y-6">
            <h3 className="text-2xl font-semibold">
              Computer Science Student & AI Enthusiast
            </h3>

            <p className="text-muted-foreground">
              I'm a Computer Science and Engineering student at The Ohio State University 
              with a 3.6 GPA, specializing in Software Engineering. I'm passionate about 
              AI, Machine Learning, and cloud technologies, with hands-on experience in 
              AWS services and full-stack development. I've completed internships at NMT Security 
              and Deloitte, gaining valuable industry experience.
            </p>

            <p className="text-muted-foreground">
              I've published research on "Fake News Detection using Machine Learning and 
              Natural Language Processing" with 22 citations and have practical experience 
              building AI chatbots, cybersecurity tools, and full-stack applications. I'm 
              also the founder and president of The Quantum Computing Club at Ohio State, 
              and serve as a Resident Advisor with a 95% satisfaction rate.
            </p>

            <div className="flex flex-col sm:flex-row gap-4 pt-4 justify-center">
              <a href="#contact" className="cosmic-button">
                {" "}
                Get In Touch
              </a>

              <a
                href="/resume.pdf"
                download="Ansh_Pachauri_Resume.pdf"
                className="px-6 py-2 rounded-full border border-primary text-primary hover:bg-primary/10 transition-colors duration-300"
              >
                Download CV
              </a>
            </div>
          </div>

          <div className="grid grid-cols-1 gap-6">
            <div className="gradient-border p-6 card-hover">
              <div className="flex items-start gap-4">
                <div className="p-3 rounded-full bg-primary/10">
                  <Code className="h-6 w-6 text-primary" />
                </div>
                <div className="text-left">
                  <h4 className="font-semibold text-lg">AI & Machine Learning</h4>
                  <p className="text-muted-foreground">
                    Building intelligent systems with Python, TensorFlow, and AWS 
                    services for real-world applications.
                  </p>
                </div>
              </div>
            </div>
            <div className="gradient-border p-6 card-hover">
              <div className="flex items-start gap-4">
                <div className="p-3 rounded-full bg-primary/10">
                  <User className="h-6 w-6 text-primary" />
                </div>
                <div className="text-left">
                  <h4 className="font-semibold text-lg">Full-Stack Development</h4>
                  <p className="text-muted-foreground">
                    Building end-to-end applications with React, Node.js, Ruby on Rails, 
                    and cloud technologies.
                  </p>
                </div>
              </div>
            </div>
            <div className="gradient-border p-6 card-hover">
              <div className="flex items-start gap-4">
                <div className="p-3 rounded-full bg-primary/10">
                  <Briefcase className="h-6 w-6 text-primary" />
                </div>

                <div className="text-left">
                  <h4 className="font-semibold text-lg">Leadership & Research</h4>
                  <p className="text-muted-foreground">
                    Leading quantum computing initiatives and conducting research 
                    with published papers and conference presentations.
                  </p>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>
  );
};
