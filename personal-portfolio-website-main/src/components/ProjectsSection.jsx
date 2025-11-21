import { ArrowRight, ExternalLink, Github } from "lucide-react";

const projects = [
  {
    id: 1,
    title: "RecipeShare - Social Recipe Network",
    description: "Instagram-inspired social media platform for recipes with account management, follow system, real-time notifications, commenting, and advanced search features.",
    image: "/projects/project1.png",
    tags: ["Next.js", "TypeScript", "Go", "PostgreSQL", "AWS S3"],
    demoUrl: "https://recipe-social-media-personal.vercel.app/auth/login",
    githubUrl: "https://github.com/anshpachauri23/Portfolio",
  },
  {
    id: 2,
    title: "E-commerce Platform",
    description: "Full-stack e-commerce solution with backend infrastructure using Node.js, AWS Lambda, DynamoDB, and API Gateway, featuring complete buying workflow and user experience optimization.",
    image: "/projects/project2.png",
    tags: ["React.js", "Node.js", "AWS Lambda", "DynamoDB"],
    demoUrl: "#",
    githubUrl: "https://github.com/anshpachauri23/Portfolio",
  },
  {
    id: 3,
    title: "PeerEval - Academic Assessment Platform",
    description: "Full-stack CRUD platform for 200+ students built with Ruby on Rails 7.2 and SQLite3, reducing submission errors by 35% and improving SQL query speeds by 2s/request.",
    image: "/projects/project3.png",
    tags: ["Ruby on Rails 7.2", "SQLite3", "Bootstrap 5", "Full-Stack"],
    demoUrl: "#",
    githubUrl: "https://github.com/anshpachauri23/Portfolio",
  },
  {
    id: 4,
    title: "Core Language Interpreter",
    description: "Comprehensive interpreter with lexical analysis, parsing, and recursive-descent execution featuring robust memory management and garbage collection.",
    image: null,
    tags: ["Java", "Compiler Design", "Memory Management"],
    demoUrl: "#",
    githubUrl: "https://github.com/anshpachauri23/Portfolio",
  },
  {
    id: 5,
    title: "Machine Learning & AI Class Projects",
    description: "Engineered 4 major machine learning algorithms from scratch using Python and NumPy, including search algorithms (DFS, BFS, UCS, A*), linear regression, Naive Bayes classification, and neural networks, achieving 74%+ accuracy on Twitter sentiment analysis.",
    image: null,
    tags: ["Python", "Machine Learning", "NumPy", "Neural Networks"],
    demoUrl: "#",
    githubUrl: "https://github.com/anshpachauri23/Portfolio",
  },
];

export const ProjectsSection = () => {
  return (
    <section id="projects" className="py-24 px-4 relative">
      <div className="container mx-auto max-w-5xl">
        <h2 className="text-3xl md:text-4xl font-bold mb-4 text-center">
          {" "}
          Featured <span className="text-primary"> Projects </span>
        </h2>

        <p className="text-center text-muted-foreground mb-12 max-w-2xl mx-auto">
          Here are some of my recent projects. Each project was carefully
          crafted with attention to detail, performance, and user experience.
        </p>

        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-8">
          {projects.map((project, key) => (
            <div
              key={key}
              className="group bg-card rounded-lg overflow-hidden shadow-xs card-hover"
            >
              {project.image && (
                <div className="h-48 overflow-hidden">
                  <img
                    src={project.image}
                    alt={project.title}
                    className="w-full h-full object-cover transition-transform duration-500 group-hover:scale-110"
                  />
                </div>
              )}

              <div className="p-6">
                <h3 className="text-xl font-semibold mb-1"> {project.title}</h3>
                <p className="text-muted-foreground text-sm mb-4">
                  {project.description}
                </p>
                
                <div className="flex flex-wrap gap-2 mb-4">
                  {project.tags.map((tag) => (
                    <span className="px-2 py-1 text-xs font-medium border rounded-full bg-secondary text-secondary-foreground">
                      {tag}
                    </span>
                  ))}
                </div>
                <div className="flex justify-between items-center">
                  <div className="flex space-x-3">
                    <a
                      href={project.demoUrl}
                      target="_blank"
                      className="text-foreground/80 hover:text-primary transition-colors duration-300"
                    >
                      <ExternalLink size={20} />
                    </a>
                    <a
                      href={project.githubUrl}
                      target="_blank"
                      className="text-foreground/80 hover:text-primary transition-colors duration-300"
                    >
                      <Github size={20} />
                    </a>
                  </div>
                </div>
              </div>
            </div>
          ))}
        </div>

        <div className="text-center mt-12">
          <a
            className="cosmic-button w-fit flex items-center mx-auto gap-2"
            target="_blank"
            href="https://github.com/anshpachauri23/Portfolio"
          >
            Check My Github <ArrowRight size={16} />
          </a>
        </div>
      </div>
    </section>
  );
};
