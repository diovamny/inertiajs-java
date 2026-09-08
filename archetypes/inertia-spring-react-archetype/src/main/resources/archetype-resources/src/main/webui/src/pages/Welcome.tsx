export default function Welcome(props: { appName: string; framework: string; frameworkVersion: string; inertiaUrl: string }) {
  return (
    <main className="welcome">
      <img className="logo" src="/brand/inertia.svg" alt="Inertia.js" />
      <h1>I <span role="img" aria-label="love">♥</span> Inertia.js</h1>
      <p>{props.appName} powered by {props.framework} {props.frameworkVersion}</p>
      <p><a href={props.inertiaUrl} target="_blank" rel="noreferrer">Visit Inertia.js</a></p>
      <div className="powered">
        <img src="/brand/spring.svg" alt="Spring" />
        <span>Powered by Spring Boot</span>
      </div>
      <div className="powered">
        <img src="/brand/react.svg" alt="React" />
        <span>Built with React</span>
      </div>
      <div className="powered">
        <img src="/brand/java.svg" alt="Java" />
        <span>Running on Java</span>
      </div>
    </main>
  )
}
