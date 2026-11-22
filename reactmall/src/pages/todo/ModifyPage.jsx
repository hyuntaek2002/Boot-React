import { useParams } from "react-router-dom";

const ModifyPage = () => {
  const { tno } = useParams()

  return (
    <div className="text-4xl">
      Todo Modify Page {tno}
    </div>
  );
}

export default ModifyPage;
